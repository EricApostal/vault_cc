package net.automationmc;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public final class VaultCC {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "vault_cc";
    private static VaultManager vaultManager;

    public static void init() {
    }

    public static void onServerStarted() {
        LOGGER.info("Running Vault CC Integration");
        vaultManager = new VaultManager();
        VaultManager.TransactionResponse bal = vaultManager.getPlayerBalance("SirTZN");

        LOGGER.info("Player balance: " + bal.balance);
        vaultManager.depositPlayer("SirTZN", 1000);
        LOGGER.info("Player balance #2: " + vaultManager.getPlayerBalance("SirTZN").balance);
    }
}
