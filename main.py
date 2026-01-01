from fastapi import FastAPI, UploadFile, File, HTTPException
from pydantic import BaseModel
import shutil
import os

from langchain_community.document_loaders import PyPDFLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_openai import OpenAIEmbeddings
from langchain_chroma import Chroma
from langchain_openai import ChatOpenAI
from langchain_classic.chains.retrieval_qa.base import RetrievalQA

app = FastAPI()

os.environ["OPENAI_API_KEY"] = "YOUR-API-KEY"

persist_directory = "./chroma_db"
embeddings = OpenAIEmbeddings()
vector_store = Chroma(persist_directory=persist_directory, embedding_function=embeddings)

class QueryRequest(BaseModel):
    question: str

@app.post("/upload")
async def upload_document(file: UploadFile = File(...)):
    """Handles PDF upload, splits text, and saves to Vector DB"""
    try:
        file_path = f"temp_{file.filename}"
        with open(file_path, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)

        loader = PyPDFLoader(file_path)
        pages = loader.load_and_split()
        
        text_splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=100)
        docs = text_splitter.split_documents(pages)

        vector_store.add_documents(docs)
        
        os.remove(file_path)
        
        return {"status": "success", "message": "Document processed and memorized."}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/chat")
async def chat_with_doc(request: QueryRequest):
    """Retrieves relevant context and answers the question"""
    
    llm = ChatOpenAI(model_name="gpt-4o", temperature=0)
    qa_chain = RetrievalQA.from_chain_type(
        llm=llm,
        chain_type="stuff",
        retriever=vector_store.as_retriever(search_kwargs={"k": 3}),
        return_source_documents=True
    )

    result = qa_chain.invoke({"query": request.question})
    
    sources = [doc.metadata.get('page', 0) for doc in result['source_documents']]
    
    return {
        "answer": result['result'],
        "citations": list(set(sources)) 
    }