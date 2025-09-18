#!/bin/bash

echo "--------------- 서버 배포 시작 -----------------"
#Parameter Store에서 모든 환경변수 가져오기
DB_URL=$(aws ssm get-parameter --name "/talktitude/db-url" --with-decryption --query "Parameter.Value" --output text)
DB_USERNAME=$(aws ssm get-parameter --name "/talktitude/db-username" --with-decryption --query "Parameter.Value" --output text)
DB_PASSWORD=$(aws ssm get-parameter --name "/talktitude/db-password" --with-decryption --query "Parameter.Value" --output text)
OPENAI_API_KEY=$(aws ssm get-parameter --name "/talktitude/openai-api-key" --with-decryption --query "Parameter.Value" --output text)
JWT_SECRET_KEY=$(aws ssm get-parameter --name "/talktitude/jwt-secret" --with-decryption --query "Parameter.Value" --output text)
AWS_ACCESS_KEY_ID=$(aws ssm get-parameter --name "/talktitude/aws-access-key" --with-decryption --query "Parameter.Value" --output text)
AWS_SECRET_ACCESS_KEY=$(aws ssm get-parameter --name "/talktitude/aws-secret-key" --with-decryption --query "Parameter.Value" --output text)
AWS_REGION=$(aws ssm get-parameter --name "/talktitude/aws-region" --query "Parameter.Value" --output text)
AWS_BUCKET=$(aws ssm get-parameter --name "/talktitude/aws-bucket" --query "Parameter.Value" --output text)
ECR_REGISTRY=$(aws ssm get-parameter --name "/talktitude/ecr-registry" --query "Parameter.Value" --output text)
AWS_BUCKET_ONNX=$(aws ssm get-parameter --name "/talktitude/aws-bucket-onnx" --query "Parameter.Value" --output text)


echo "ECR 레지스트리: $ECR_REGISTRY"


aws ecr get-login-password --region ap-northeast-2 | \
    sudo docker login --username AWS --password-stdin $ECR_REGISTRY
# 기존 컨테이너 정리
sudo docker stop talktitude || true
sudo docker rm talktitude || true

# 최신 이미지 pull
sudo docker pull $ECR_REGISTRY/talktitude:latest

sudo docker run -d \
  --name talktitude \
  -p 8080:8080 \
  -e DB_URL="$DB_URL" \
  -e DB_USERNAME="$DB_USERNAME" \
  -e DB_PASSWORD="$DB_PASSWORD" \
  -e OPENAI_API_KEY="$OPENAI_API_KEY" \
  -e OPENAI_BASE_URL="https://api.openai.com" \
  -e JWT_ISSUER="talktitude" \
  -e JWT_SECRET_KEY="$JWT_SECRET_KEY" \
  -e AWS_ACCESS_KEY_ID="$AWS_ACCESS_KEY_ID" \
  -e AWS_SECRET_ACCESS_KEY="$AWS_SECRET_ACCESS_KEY" \
  -e AWS_DEFAULT_REGION="ap-northeast-2" \
  -e AWS_REGION="$AWS_REGION" \
  -e AWS_BUCKET="$AWS_BUCKET" \
  -e AWS_BUCKET_ONNX ="$AWS_BUCKET_ONNX"
  $ECR_REGISTRY/talktitude:latest

echo "--------------- 서버 배포 완료 -----------------"
