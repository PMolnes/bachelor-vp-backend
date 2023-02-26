package no.ntnu.bachelor.voicepick.features.authentication.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import ch.qos.logback.core.subst.Token;
import lombok.RequiredArgsConstructor;
import no.ntnu.bachelor.voicepick.features.authentication.dtos.LoginRequest;
import no.ntnu.bachelor.voicepick.features.authentication.dtos.LoginResponse;
import no.ntnu.bachelor.voicepick.features.authentication.dtos.TokenRequest;
import no.ntnu.bachelor.voicepick.features.authentication.dtos.SignupRequest;
import no.ntnu.bachelor.voicepick.features.authentication.services.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    ResponseEntity<?> response;

    try {
      LoginResponse loginResponse = this.authService.login(request);
      response = new ResponseEntity<LoginResponse>(loginResponse, HttpStatus.OK);
    } catch (Exception e) {
      response = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    return response;
  }

  @PostMapping("/signup")
  public ResponseEntity<String> signup(@RequestBody SignupRequest request) throws Exception {
    ResponseEntity<String> response;

    try {
      this.authService.signup(request);
      response = new ResponseEntity<>(HttpStatus.OK);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.CONFLICT) {
        response = new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
      } else {
        response = new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
      }
    } catch (Exception e) {
      response = new ResponseEntity<>("Something went wrong! Please try again.", HttpStatus.BAD_REQUEST);
    }

    return response;
  }

  @PostMapping("/signout")
  public ResponseEntity<String> signout(@RequestBody TokenRequest request) {
    if (this.authService.signout(request)) {
      return new ResponseEntity<>("Signed out successfully", HttpStatus.OK);
    } else {
      return new ResponseEntity<>("Something went wrong! Could not sign you out.", HttpStatus.BAD_REQUEST);
    }
  }

  @PostMapping("/introspect")
  public ResponseEntity<String> introspect(@RequestBody TokenRequest request) {
    if (this.authService.introspect(request)) {
      return new ResponseEntity<>("Token is active", HttpStatus.OK);
    } else {
      return new ResponseEntity<>("Token is not active", HttpStatus.UNAUTHORIZED);
    }
  }

}
