import java.io.InputStream;
import java.net.URL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;

public class JsonValidator
{
  protected JsonNode getJsonNodeFromClasspath(final String name) throws Exception
  {
    final InputStream is1 = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);

    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode node = mapper.readTree(is1);
    return node;
  }

  protected JsonNode getJsonNodeFromStringContent(final String content) throws Exception
  {
    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode node = mapper.readTree(content);
    return node;
  }

  protected JsonNode getJsonNodeFromUrl(final String url) throws Exception
  {
    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode node = mapper.readTree(new URL(url));
    return node;
  }

  protected JsonSchema getJsonSchemaFromClasspath(final String name) throws Exception
  {
    final JsonSchemaFactory factory = new JsonSchemaFactory();
    final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    final JsonSchema schema = factory.getSchema(is);
    return schema;
  }

  protected JsonSchema getJsonSchemaFromStringContent(final String schemaContent) throws Exception
  {
    final JsonSchemaFactory factory = new JsonSchemaFactory();
    final JsonSchema schema = factory.getSchema(schemaContent);
    return schema;
  }

  protected JsonSchema getJsonSchemaFromUrl(final String url) throws Exception
  {
    final JsonSchemaFactory factory = new JsonSchemaFactory();
    final JsonSchema schema = factory.getSchema(new URL(url));
    return schema;
  }

  protected JsonSchema getJsonSchemaFromJsonNode(final JsonNode jsonNode) throws Exception
  {
    final JsonSchemaFactory factory = new JsonSchemaFactory();
    final JsonSchema schema = factory.getSchema(jsonNode);
    return schema;
  }
}