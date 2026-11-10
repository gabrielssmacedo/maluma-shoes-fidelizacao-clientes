# frontend/Dockerfile
FROM node:20-alpine

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia os arquivos de dependências primeiro (otimiza o cache do Docker)
COPY package.json package-lock.json ./

# Instala as dependências
RUN npm install

# Copia o restante do código do frontend
COPY . .

# Expõe a porta que o Vite utiliza
EXPOSE 5173

# Comando para iniciar o servidor de desenvolvimento
CMD ["npm", "run", "dev"]