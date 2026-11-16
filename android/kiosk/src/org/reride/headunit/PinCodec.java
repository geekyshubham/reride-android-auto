package org.reride.headunit;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
/** Salted password derivation; no plaintext PIN or recovery PIN is stored. */
public final class PinCodec {
 public static boolean valid(String pin) {return pin!=null && pin.matches("[0-9]{6,12}");}
 public static String encode(String pin) throws Exception {
  if(!valid(pin))throw new IllegalArgumentException("Use 6–12 digits");
  byte[] salt=new byte[16];new SecureRandom().nextBytes(salt);
  return hex(salt)+":"+hex(derive(pin,salt));
 }
 public static boolean verify(String pin,String record) throws Exception {
  if(!valid(pin)||record==null)return false;
  String[] parts=record.split(":");if(parts.length!=2)return false;
  return MessageDigest.isEqual(derive(pin,unhex(parts[0])),unhex(parts[1]));
 }
 private static byte[] derive(String pin,byte[] salt)throws Exception {
  PBEKeySpec spec=new PBEKeySpec(pin.toCharArray(),salt,120000,256);
  try{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).getEncoded();}finally{spec.clearPassword();}
 }
 private static String hex(byte[] bytes){StringBuilder s=new StringBuilder();for(byte b:bytes)s.append(String.format("%02x",b&255));return s.toString();}
 private static byte[] unhex(String s){if(s.length()%2!=0)throw new IllegalArgumentException();byte[] b=new byte[s.length()/2];for(int i=0;i<b.length;i++)b[i]=(byte)Integer.parseInt(s.substring(2*i,2*i+2),16);return b;}
}
