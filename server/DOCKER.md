## Backend Docker Run Guide

### 1) Prepare env file
```bash
cd server
cp .env.example .env
# edit .env and set real values
```

### 2) Build and run
```bash
docker compose up --build -d
```

Backend will be available at `http://localhost:8080`.

### 3) View logs
```bash
docker compose logs -f backend
```

### 4) Stop
```bash
docker compose down
```

## Notes
- If using local MongoDB from inside Docker on Linux, keep:
  `MONGODB_URI=mongodb://host.docker.internal:27017/invoicemanager`
- For MongoDB Atlas, use full SRV URI with DB name in `.env`.
- Do not commit `.env`.
