import requests
import concurrent.futures
import uuid

def send_request(url: str):
    payload = {"originalUrl": f"http://example.com/{uuid.uuid4()}"}
    try:
        response = requests.post(url, json=payload)
        print(f"Status: {response.status_code}, result: {response.content}")
    except Exception as e:
        print(f"Request failed: {e}")



# Configuration
URL = "http://localhost:8080/api/v1/shorten"
TOTAL_REQUESTS = 1000
CONCURRENT_THREADS = 10

# Using the ThreadPoolExecutor
with concurrent.futures.ThreadPoolExecutor(max_workers=CONCURRENT_THREADS) as executor:
    # Schedule the requests
    futures = [executor.submit(send_request, URL) for _ in range(TOTAL_REQUESTS)]
    
    # Optional: Wait for completion and handle results
    concurrent.futures.wait(futures)