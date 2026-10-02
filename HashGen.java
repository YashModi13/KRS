import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGen {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String pass1 = "Admin@1310";
        String hash1 = encoder.encode(pass1);
        System.out.println("RAW_PASSWORD: " + pass1);
        System.out.println("BCRYPT_HASH: " + hash1);
        System.out.println("MATCHES: " + encoder.matches(pass1, hash1));
    }
}
