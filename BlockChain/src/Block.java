import java.io.Serializable;
import java.util.Date;

public class Block implements Serializable {
    private String hash;
    private final String previousHash;
    private final String data;
    private final long timeStamp;
    private int nonce;

    public Block(String data, String previousHash) {
        this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public String getData() {
        return data;
    }

    public String getHash() {
        return hash;
    }
    public String calculateHash() {
        return StringUtil.applySha256(data + previousHash + timeStamp + nonce);
    }

    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0');
        while (!hash.substring(0, difficulty).equals(target)) {
            ++nonce;
            hash = calculateHash();
        }
        System.out.println("Block mined. " + hash);
    }
}