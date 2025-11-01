import com.ghostvault.ui.VaultMainController;
import com.ghostvault.security.PasswordVaultManager;
import com.ghostvault.security.SecureNotesManager;
import com.ghostvault.model.VaultFile;

public class TestCompilation {
    public static void main(String[] args) {
        System.out.println("Testing compilation...");
        
        // Test that classes can be instantiated
        VaultMainController controller = new VaultMainController();
        System.out.println("VaultMainController: OK");
        
        PasswordVaultManager passwordManager = new PasswordVaultManager("test");
        System.out.println("PasswordVaultManager: OK");
        
        SecureNotesManager notesManager = new SecureNotesManager("test");
        System.out.println("SecureNotesManager: OK");
        
        System.out.println("All classes compiled successfully!");
    }
}