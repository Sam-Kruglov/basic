groupId=com.samkruglov
artifactId=base-client
version=0.1.0
package=$groupId.base.client.gen

# let it re-generate all the markdown files. By default it will not replace existing ones
rm -rf $artifactId/**/*.md

# https://github.com/google/google-java-format
formatter_version=1.9
formatter=google-java-format-$formatter_version-all-deps.jar
./mvnw org.apache.maven.plugins:maven-dependency-plugin:3.1.2:copy \
  -DoutputDirectory=. \
  -Dartifact=com.google.googlejavaformat:google-java-format:$formatter_version:jar:all-deps
export JAVA_POST_PROCESS_FILE="java -jar $formatter --replace"
openapi-generator generate \
  --generator-name java \
  --output $artifactId \
  --group-id $groupId \
  --artifact-id $artifactId \
  --artifact-version $version \
  --api-package $package.api \
  --model-package $package.view \
  --enable-post-process-file \
  --input-spec http://localhost:8080/open-api/v3/api-docs \
  --additional-properties hideGenerationTimestamp=true \
  --additional-properties booleanGetterPrefix=is \
  --additional-properties dateLibrary=java8 \
  --additional-properties useRuntimeException=true \
  --additional-properties library=feign
rm -f $formatter
rm -rf $artifactId/gradle # empty directory still gets created