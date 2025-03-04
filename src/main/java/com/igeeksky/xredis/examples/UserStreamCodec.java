package com.igeeksky.xredis.examples;

import com.igeeksky.xredis.common.stream.StreamCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2025/3/4
 */
public class UserStreamCodec implements StreamCodec<String, String, User> {

    @Override
    public Map<String, String> encodeMsg(User user) {
        Objects.requireNonNull(user, "user must not be null");
        Map<String, String> body = HashMap.newHashMap(3);
        Long id = user.getId();
        if (id != null) {
            body.put("id", id.toString());
        }
        if (user.getName() != null) {
            body.put("name", user.getName());
        }
        Integer age = user.getAge();
        if (age != null) {
            body.put("age", age.toString());
        }
        return body;
    }

    @Override
    public User decodeMsg(Map<String, String> body) {
        if (body == null) {
            return null;
        }
        User user = new User();
        String id = body.get("id");
        if (id != null) {
            user.setId(Long.valueOf(id));
        }
        String name = body.get("name");
        if (name != null) {
            user.setName(name);
        }
        String age = body.get("age");
        if (age != null) {
            user.setAge(Integer.valueOf(age));
        }
        return user;
    }

}
