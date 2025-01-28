package net.automationmc;

import com.mojang.logging.LogUtils;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

import java.lang.reflect.Method;

/**
 * A reflection-based wrapper for Vault economy operations that allows for using
 * Vault functionality without direct dependencies. This manager handles all economy
 * operations including balance checks, deposits, withdrawals, and bank operations.
 */
public class VaultManager {
    private Object economyProvider;
    private Class<?> economyClass;
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Represents the result of an economy transaction, mirroring Vault's EconomyResponse.
     * This class allows us to handle transaction results without direct Vault dependencies.
     */
    public static class TransactionResponse {
        public final boolean success;
        public final String errorMessage;
        public final double amount;
        public final double balance;

        public TransactionResponse(boolean success, String errorMessage, double amount, double balance) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.amount = amount;
            this.balance = balance;
        }
    }

    public VaultManager() {
        setupEconomy();
    }

    /**
     * Get the balance of a player.
     * @param playerName The name of the player to get the balance of.
     * @return TransactionResponse containing the player's balance.
     */
    public TransactionResponse getPlayerBalance(String playerName) {
        try {
            Method getBalanceMethod = economyClass.getMethod("getBalance",
                    org.bukkit.OfflinePlayer.class);

            double balance = (double) getBalanceMethod.invoke(
                    economyProvider,
                    Bukkit.getOfflinePlayer(playerName)
            );

            return new TransactionResponse(true, null, 0.0, balance);
        } catch (Exception e) {
            LOGGER.error("Error getting player balance: ", e);
            return new TransactionResponse(false, e.getMessage(), 0.0, 0.0);
        }
    }

    /**
     * Check if a player has an account in the economy plugin.
     * @param playerName The name of the player to check.
     * @return TransactionResponse containing the result. Check success field for result.
     */
    public TransactionResponse playerHasAccount(String playerName) {
        try {
            Method hasAccountMethod = economyProvider.getClass()
                    .getMethod("hasAccount", org.bukkit.OfflinePlayer.class);

            boolean hasAccount = (boolean) hasAccountMethod.invoke(
                    economyProvider,
                    Bukkit.getOfflinePlayer(playerName)
            );

            return new TransactionResponse(hasAccount, null, 0.0, 0.0);
        } catch (Exception e) {
            LOGGER.error("Error checking if player has account: ", e);
            return new TransactionResponse(false, e.getMessage(), 0.0, 0.0);
        }
    }

    /**
     * Deposits money into a player's account.
     * @param playerName The name of the player to deposit to
     * @param amount The amount to deposit
     * @return TransactionResponse containing the result of the operation
     */
    public TransactionResponse depositPlayer(String playerName, double amount) {
        try {
            Method depositPlayerMethod = economyClass.getMethod("depositPlayer",
                    org.bukkit.OfflinePlayer.class,
                    double.class);

            Object response = depositPlayerMethod.invoke(
                    economyProvider,
                    Bukkit.getOfflinePlayer(playerName),
                    amount
            );

            return parseEconomyResponse(response);
        } catch (Exception e) {
            LOGGER.error("Error depositing to player account: ", e);
            return new TransactionResponse(false, e.getMessage(), amount, getPlayerBalance(playerName).balance);
        }
    }

    /**
     * Withdraws money from a player's account.
     * @param playerName The name of the player to withdraw from
     * @param amount The amount to withdraw
     * @return TransactionResponse containing the result of the operation
     */
    public TransactionResponse withdrawPlayer(String playerName, double amount) {
        try {
            Method withdrawPlayerMethod = economyClass.getMethod("withdrawPlayer",
                    org.bukkit.OfflinePlayer.class,
                    double.class);

            Object response = withdrawPlayerMethod.invoke(
                    economyProvider,
                    Bukkit.getOfflinePlayer(playerName),
                    amount
            );

            return parseEconomyResponse(response);
        } catch (Exception e) {
            LOGGER.error("Error withdrawing from player account: ", e);
            return new TransactionResponse(false, e.getMessage(), amount, getPlayerBalance(playerName).balance);
        }
    }

    /**
     * Check if a player has enough money.
     * @param playerName The name of the player to check
     * @param amount The amount to check for
     * @return TransactionResponse containing the result. Check success field for result.
     */
    public TransactionResponse has(String playerName, double amount) {
        try {
            Method hasMethod = economyClass.getMethod("has",
                    org.bukkit.OfflinePlayer.class,
                    double.class);

            boolean hasAmount = (boolean) hasMethod.invoke(
                    economyProvider,
                    Bukkit.getOfflinePlayer(playerName),
                    amount
            );

            double balance = getPlayerBalance(playerName).balance;
            return new TransactionResponse(hasAmount, null, amount, balance);
        } catch (Exception e) {
            LOGGER.error("Error checking if player has amount: ", e);
            return new TransactionResponse(false, e.getMessage(), amount, 0.0);
        }
    }

    /**
     * Creates a bank account.
     * @param bankName The name of the bank account
     * @param playerName The owner of the bank account
     * @return TransactionResponse containing the result of the operation
     */
    public TransactionResponse createBank(String bankName, String playerName) {
        try {
            Method createBankMethod = economyClass.getMethod("createBank",
                    String.class,
                    org.bukkit.OfflinePlayer.class);

            Object response = createBankMethod.invoke(
                    economyProvider,
                    bankName,
                    Bukkit.getOfflinePlayer(playerName)
            );

            return parseEconomyResponse(response);
        } catch (Exception e) {
            LOGGER.error("Error creating bank account: ", e);
            return new TransactionResponse(false, e.getMessage(), 0.0, 0.0);
        }
    }

    /**
     * Gets the balance of a bank account.
     * @param bankName The name of the bank account
     * @return TransactionResponse containing the bank's balance
     */
    public TransactionResponse getBankBalance(String bankName) {
        try {
            Method bankBalanceMethod = economyClass.getMethod("bankBalance",
                    String.class);

            double balance = (double) bankBalanceMethod.invoke(economyProvider, bankName);
            return new TransactionResponse(true, null, 0.0, balance);
        } catch (Exception e) {
            LOGGER.error("Error getting bank balance: ", e);
            return new TransactionResponse(false, e.getMessage(), 0.0, 0.0);
        }
    }

    /**
     * Format the amount according to the economy provider's settings.
     * @param amount The amount to format
     * @return The formatted amount as a string
     */
    public String format(double amount) {
        try {
            Method formatMethod = economyClass.getMethod("format",
                    double.class);

            return (String) formatMethod.invoke(economyProvider, amount);
        } catch (Exception e) {
            LOGGER.error("Error formatting amount: ", e);
            return String.format("%.2f", amount);
        }
    }

    /**
     * Gets the name of the currency in singular form.
     * @return The name of the currency
     */
    public String getCurrencyNameSingular() {
        try {
            Method currencyNameMethod = economyClass.getMethod("currencyNameSingular");
            return (String) currencyNameMethod.invoke(economyProvider);
        } catch (Exception e) {
            LOGGER.error("Error getting currency name: ", e);
            return "dollar";
        }
    }

    /**
     * Gets the name of the currency in plural form.
     * @return The name of the currency
     */
    public String getCurrencyNamePlural() {
        try {
            Method currencyNameMethod = economyClass.getMethod("currencyNamePlural");
            return (String) currencyNameMethod.invoke(economyProvider);
        } catch (Exception e) {
            LOGGER.error("Error getting currency name: ", e);
            return "dollars";
        }
    }

    /**
     * Helper method to parse the EconomyResponse object from Vault
     */
    private TransactionResponse parseEconomyResponse(Object economyResponse) {

        // I don't really like this very much but claude wrote it works well enough
        try {
            Class<?> responseClass = economyResponse.getClass();

            // Try to determine the type of response and extract relevant information
            // First attempt: Check if it's a boolean response
            if (economyResponse instanceof Boolean) {
                return new TransactionResponse(
                        (Boolean) economyResponse,
                        null,
                        0.0,
                        0.0
                );
            }

            // Second attempt: Try to get common response fields using reflection
            boolean success = false;
            String errorMessage = null;
            double amount = 0.0;
            double balance = 0.0;

            try {
                success = (boolean) getFieldValue(responseClass, economyResponse,
                        new String[]{"transactionSuccess", "success", "isSuccess"});
            } catch (Exception e) {
                // If we can't find a success field, assume success if we got a response
                success = true;
            }

            try {
                errorMessage = (String) getFieldValue(responseClass, economyResponse,
                        new String[]{"errorMessage", "error", "message"});
            } catch (Exception ignored) {}

            try {
                amount = (double) getFieldValue(responseClass, economyResponse,
                        new String[]{"amount", "value", "transferAmount"});
            } catch (Exception ignored) {}

            try {
                balance = (double) getFieldValue(responseClass, economyResponse,
                        new String[]{"balance", "newBalance", "currentBalance"});
            } catch (Exception ignored) {}

            return new TransactionResponse(success, errorMessage, amount, balance);
        } catch (Exception e) {
            LOGGER.error("Error parsing economy response: ", e);
            return new TransactionResponse(true, null, 0.0, 0.0);
        }
    }

    /**
     * Helper method to try multiple field names when accessing response fields
     */
    private Object getFieldValue(Class<?> clazz, Object obj, String[] fieldNames) throws Exception {
        for (String fieldName : fieldNames) {
            try {
                return clazz.getField(fieldName).get(obj);
            } catch (NoSuchFieldException ignored) {
                try {
                    Method getter = clazz.getMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
                    return getter.invoke(obj);
                } catch (NoSuchMethodException ignored2) {}
            }
        }
        throw new NoSuchFieldException("None of the specified fields found: " + String.join(", ", fieldNames));
    }

    /**
     * Sets up the economy connection using reflection.
     * This method initializes the connection to Vault and the economy provider.
     */
    private void setupEconomy() {
        try {
            org.bukkit.plugin.Plugin vaultPlugin = Bukkit.getPluginManager().getPlugin("Vault");
            if (vaultPlugin == null) {
                LOGGER.error("Vault plugin not found. Please ensure Vault is installed.");
                return;
            }

            Object servicesManager = Bukkit.getServicesManager();

            economyClass = Class.forName(
                    "net.milkbowl.vault.economy.Economy",
                    true,
                    vaultPlugin.getClass().getClassLoader()
            );

            Method getRegistrationMethod = servicesManager.getClass()
                    .getMethod("getRegistration", Class.class);

            Object rsp = getRegistrationMethod.invoke(servicesManager, economyClass);

            if (rsp != null) {
                Method getProviderMethod = rsp.getClass().getMethod("getProvider");
                Object provider = getProviderMethod.invoke(rsp);

                if (provider != null) {
                    this.economyProvider = provider;

                    LOGGER.info("Successfully connected to Vault economy!");
                    LOGGER.info("Provider: " + provider.getClass().getName());
                } else {
                    LOGGER.error("Economy provider is null!");
                }
            } else {
                LOGGER.error("No economy provider found! Make sure an economy plugin (ex: Essentials) is installed.");
            }
        } catch (Exception e) {
            LOGGER.error("Error setting up economy connection: ", e);
            e.printStackTrace();
        }
    }
}