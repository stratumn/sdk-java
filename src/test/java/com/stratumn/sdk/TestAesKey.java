package com.stratumn.sdk;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.util.Base64;

import org.junit.Test;

public class TestAesKey {

  @Test
  public void testEncrypt() {
    byte[] message = "coucou, tu veux voir mon message ?".getBytes();

    AesKey k = new AesKey();

    try {
      ByteBuffer ct = k.encrypt(ByteBuffer.wrap(message));

      ct.position(0);

      ByteBuffer bb = k.decrypt(ct);
      byte[] b = new byte[bb.remaining()];
      bb.get(b);
      assertArrayEquals(message, b);

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testDecrypt() {
    String key = "dXRdc1KYm8DVFFxc0Hq65ZVoZvHAD/PBx0GUgSMmPEw=";
    byte[] ct = Base64.getDecoder()
        .decode("FfogaZ5Wy4oDfCDqQQUtciiZf/6CsZxrBQr2ZHVswimxB7IwQw9Z8brNocu3O5q1DKYaP4cBmzcPi++1mE4=");
    byte[] message = "coucou, tu veux voir mon message ?".getBytes();

    AesKey k = new AesKey(key);

    try {
      ByteBuffer bb = k.decrypt(ByteBuffer.wrap(ct));
      byte[] b = new byte[bb.remaining()];
      bb.get(b);
      assertArrayEquals(message, b);

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

}
