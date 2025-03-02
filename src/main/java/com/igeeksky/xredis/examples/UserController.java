package com.igeeksky.xredis.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * @author Patrick.Lau
 * @since 1.0.0
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public CompletableFuture<Response<Void>> addUser(@RequestBody User user) {
        if (log.isDebugEnabled()) {
            log.debug("addUser:{}", user);
        }
        if (user == null) {
            return CompletableFuture.completedFuture(Response.error("user must not be null"));
        }
        return userService.addUser(user);
    }

    @GetMapping("/get/{id}")
    public CompletableFuture<Response<User>> getUser(@PathVariable("id") Long id) {
        if (log.isDebugEnabled()) {
            log.debug("getUser:{}", id);
        }
        if (id == null) {
            return CompletableFuture.completedFuture(Response.error("id must not be null"));
        }
        return userService.getUser(id);
    }

    @DeleteMapping("/delete/{id}")
    public CompletableFuture<Response<Void>> deleteUser(@PathVariable("id") Long id) {
        if (log.isDebugEnabled()) {
            log.debug("deleteUser:{}", id);
        }
        if (id == null) {
            return CompletableFuture.completedFuture(Response.error("id must not be null"));
        }
        return userService.deleteUser(id);
    }

}
