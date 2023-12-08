# Export Arangodb
Export all collections inside database of ArangoDB

## Using jvm:
```bash
./graldew clean build
java -jar ./build/libs/karangoexport.jar \
  --server.endpoint=127.0.0.1:8529 \
  --server.username=root \
  --server.database=dbname \
  --output-directory=output_dir
```

## Using native executable:
```bash
./gradlew clean nativeComile
./build/native/nativeCompile/karangoexport \
  --server.endpoint=127.0.0.1:8529 \
  --server.username=root \
  --server.database=dbname \
  --output-directory=output_dir
```