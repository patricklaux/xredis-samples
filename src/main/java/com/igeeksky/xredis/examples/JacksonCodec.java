package com.igeeksky.xredis.examples;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.igeeksky.xtool.core.lang.codec.CodecException;

import java.io.IOException;
import java.util.Objects;

public class JacksonCodec<K>  {

    private final JavaType javaType;
    private final ObjectMapper mapper;

    public JacksonCodec(Class<K> type) {
        this.mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.javaType = TypeFactory.defaultInstance().constructType(type);
    }

    public JacksonCodec(ObjectMapper mapper, Class<K> type) {
        this(mapper, TypeFactory.defaultInstance().constructType(type));
    }

    public JacksonCodec(ObjectMapper mapper, JavaType javaType) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        Objects.requireNonNull(javaType, "javaType must not be null");
        this.mapper = mapper;
        this.javaType = javaType;
    }

    public String encode(K key) {
        if (null == key) {
            throw new CodecException("obj must not be null");
        }
        try {
            return mapper.writeValueAsString(key);
        } catch (JsonProcessingException e) {
            String msg = String.format("Unable to write to JSON: [%s]. %s", key, e.getMessage());
            throw new CodecException(msg, e);
        }
    }

    public K decode(String source) {
        if (null == source) {
            throw new CodecException("source must not be null");
        }
        try {
            return mapper.readValue(source, javaType);
        } catch (IOException e) {
            String msg = String.format("Unable to read from JSON: [%s]. %s", source, e.getMessage());
            throw new CodecException(msg, e);
        }
    }

}