package com.sylvain.chess.web.controller;

@org.springframework.web.bind.annotation.RestController
public class TestController {
  @org.springframework.web.bind.annotation.GetMapping("/test-alive")
  public String test() { return "Server is running!"; }
}