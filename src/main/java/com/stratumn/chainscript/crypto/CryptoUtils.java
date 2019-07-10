package com.stratumn.chainscript.crypto;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import net.i2p.crypto.eddsa.*;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveSpec;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;

public class CryptoUtils {

  static EdDSANamedCurveSpec ed25519Spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);

  public static String encodeSignature(byte[] sig) {
    return String.format("-----BEGIN MESSAGE-----\n%s\n-----END MESSAGE-----", Base64.getEncoder().encodeToString(sig));
  }

  public static byte[] decodeSignature(String sig) {
    String s = sig.replaceAll("\\n", "").replace("-----BEGIN MESSAGE-----", "").replace("-----END MESSAGE-----", "");
    return Base64.getDecoder().decode(s);
  }

  public static String sign(EdDSAPrivateKey key, byte[] message)
      throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    Signature sgr = new EdDSAEngine(MessageDigest.getInstance(ed25519Spec.getHashAlgorithm()));
    sgr.initSign(key);
    sgr.update(message);
    byte[] sig = sgr.sign();
    return CryptoUtils.encodeSignature(sig);
  }

  public static boolean verify(EdDSAPublicKey key, byte[] message, String encodedSig)
      throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    byte[] sig = CryptoUtils.decodeSignature(encodedSig);

    Signature sgr = new EdDSAEngine(MessageDigest.getInstance(ed25519Spec.getHashAlgorithm()));
    sgr.initVerify(key);
    sgr.update(message);
    return sgr.verify(sig);
  }

  // In our systems, the PKCS#8 encoded ed25519 private key actually contains the
  // public key (cf tweetnacl.js and go implementations). The java library though
  // fails if the privateKey part of the ASN1 is more than 32 bytes.
  // The long term solution would be to implement a real PEM ASN1
  // serializer/deserializer.
  // In the mean time, we manually extract the seed from the encoded key, which is
  // the 32 first bytes
  // of the privateKey section of the ASN1 encoded data.
  public static EdDSAPrivateKey decodeEd25519PrivateKey(String pem) throws InvalidKeySpecException {
    String sk = pem.replaceAll("\\n", "").replace("-----BEGIN ED25519 PRIVATE KEY-----", "")
        .replace("-----END ED25519 PRIVATE KEY-----", "");

    byte[] skBytes = Base64.getDecoder().decode(sk);
    byte[] seed = Arrays.copyOfRange(skBytes, 18, 50);

    System.out.println(ed25519Spec.getCurve().getField().getb() / 4);
    EdDSAPrivateKeySpec key = new EdDSAPrivateKeySpec(seed, ed25519Spec);
    return new EdDSAPrivateKey(key);
  }

  public static EdDSAPublicKey decodeEd25519PublicKey(String pem) throws InvalidKeySpecException {
    String pk = pem.replaceAll("\\n", "").replace("-----BEGIN ED25519 PUBLIC KEY-----", "")
        .replace("-----END ED25519 PUBLIC KEY-----", "");

    byte[] pkBytes = Base64.getDecoder().decode(pk);
    X509EncodedKeySpec encoded = new X509EncodedKeySpec(pkBytes);
    return new EdDSAPublicKey(encoded);
  }
}
