package automation;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class S3Service {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final AmazonS3 amazonS3;

  public S3Service() {
    amazonS3 = AmazonS3ClientBuilder.defaultClient();
  }

  public void writeStringToS3(String bucket, String key, String content) throws IOException {
    amazonS3.putObject(bucket, key, content);
  }

  public List<String> listObjects(String bucket, String key) {
    return amazonS3.listObjects(bucket, key).getObjectSummaries().stream().map(e -> e.getKey()).collect(Collectors.toList());
  }

  public Optional<String> getObjectContent(String bucket, String key) {
    Optional<String> fileContent = Optional.empty();
    S3Object s3Object = amazonS3.getObject(bucket, key);
    try {
      if (s3Object != null) {
        fileContent = Optional.of(IOUtils.toString(s3Object.getObjectContent(), "UTF-8"));
      }
    } catch (IOException e) {
      logger.error("Failed to read file content for bucket={} key={}", bucket, key);
    }

    return fileContent;
  }
}
