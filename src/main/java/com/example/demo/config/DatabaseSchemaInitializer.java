package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Initializer for database schema updates
 * Runs SQL migrations on application startup
 */
@Component
@DependsOnDatabaseInitialization
public class DatabaseSchemaInitializer {
    
    @Autowired
    private DataSource dataSource;
    
    @EventListener(ApplicationReadyEvent.class)
    public void initializeSchema() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // Add nguoi_ghi_nhan column to payment tables (with IF NOT EXISTS)
            try {
                statement.execute("ALTER TABLE supplier_debt_payment ADD COLUMN IF NOT EXISTS nguoi_ghi_nhan VARCHAR(255) NULL AFTER ghi_chu");
                System.out.println("✅ supplier_debt_payment schema verified (nguoi_ghi_nhan exists)");
            } catch (Exception e) {
                System.err.println("⚠️  Error checking supplier_debt_payment schema: " + e.getMessage());
            }
            
            try {
                statement.execute("ALTER TABLE customer_debt_payment ADD COLUMN IF NOT EXISTS nguoi_ghi_nhan VARCHAR(255) NULL AFTER ghi_chu");
                System.out.println("✅ customer_debt_payment schema verified (nguoi_ghi_nhan exists)");
            } catch (Exception e) {
                System.err.println("⚠️  Error checking customer_debt_payment schema: " + e.getMessage());
            }
            
            // Add bad debt columns to customer_debt table (with IF NOT EXISTS)
            try {
                statement.execute("ALTER TABLE customer_debt ADD COLUMN IF NOT EXISTS uncollectible_amount DECIMAL(15,2) DEFAULT 0.00 AFTER average_payment");
                System.out.println("✅ customer_debt schema verified (uncollectible_amount exists)");
            } catch (Exception e) {
                System.err.println("⚠️  Error checking customer_debt schema: " + e.getMessage());
            }
            
            try {
                statement.execute("ALTER TABLE customer_debt ADD COLUMN IF NOT EXISTS uncollectible_reason VARCHAR(500) NULL AFTER uncollectible_amount");
                System.out.println("✅ customer_debt schema verified (uncollectible_reason exists)");
            } catch (Exception e) {
                System.err.println("⚠️  Error checking customer_debt schema: " + e.getMessage());
            }
            
            // Add is_bad_debt column to khach_hang table (with IF NOT EXISTS)
            try {
                statement.execute("ALTER TABLE khach_hang ADD COLUMN IF NOT EXISTS is_bad_debt BOOLEAN DEFAULT FALSE AFTER ma_so_thue");
                System.out.println("✅ khach_hang schema verified (is_bad_debt exists)");
            } catch (Exception e) {
                System.err.println("⚠️  Error checking khach_hang schema: " + e.getMessage());
            }
            
            // Fix foreign key constraint on chi_tiet_phieu_xuat.hang_hoa_id to allow deletion
            try {
                // First, make the column nullable if it isn't already
                statement.execute("ALTER TABLE chi_tiet_phieu_xuat MODIFY COLUMN hang_hoa_id BIGINT NULL");
                System.out.println("✅ Made chi_tiet_phieu_xuat.hang_hoa_id nullable");
            } catch (Exception e) {
                System.out.println("ℹ️  chi_tiet_phieu_xuat.hang_hoa_id nullable status: " + e.getMessage());
            }
            
            try {
                // Then, drop the old constraint
                statement.execute("ALTER TABLE chi_tiet_phieu_xuat DROP FOREIGN KEY FK1a3gk6ui6wgch6y0shtxq135i");
                System.out.println("✅ Dropped old foreign key constraint FK1a3gk6ui6wgch6y0shtxq135i");
            } catch (Exception e) {
                if (e.getMessage().contains("constraint") || e.getMessage().contains("1091")) {
                    System.out.println("ℹ️  Foreign key FK1a3gk6ui6wgch6y0shtxq135i already removed or doesn't exist");
                } else {
                    System.err.println("⚠️  Error dropping foreign key: " + e.getMessage());
                }
            }
            
            try {
                // Recreate the constraint with ON DELETE SET NULL
                statement.execute("ALTER TABLE chi_tiet_phieu_xuat ADD CONSTRAINT FK1a3gk6ui6wgch6y0shtxq135i FOREIGN KEY (hang_hoa_id) REFERENCES hang_hoa (id) ON DELETE SET NULL");
                System.out.println("✅ Recreated foreign key constraint with ON DELETE SET NULL");
            } catch (Exception e) {
                if (e.getMessage().contains("Duplicate") || e.getMessage().contains("1022")) {
                    System.out.println("ℹ️  Foreign key constraint already exists with correct configuration");
                } else {
                    System.err.println("⚠️  Error recreating foreign key: " + e.getMessage());
                }
            }
            
            // Update test data for payment tables
            try {
                int supplierCount = statement.executeUpdate("UPDATE supplier_debt_payment SET nguoi_ghi_nhan = 'Admin User' WHERE nguoi_ghi_nhan IS NULL LIMIT 10");
                if (supplierCount > 0) {
                    System.out.println("✅ Updated " + supplierCount + " supplier_debt_payment records with test data");
                }
            } catch (Exception e) {
                System.err.println("ℹ️  Could not update test data: " + e.getMessage());
            }
            
            try {
                int customerCount = statement.executeUpdate("UPDATE customer_debt_payment SET nguoi_ghi_nhan = 'Admin User' WHERE nguoi_ghi_nhan IS NULL LIMIT 10");
                if (customerCount > 0) {
                    System.out.println("✅ Updated " + customerCount + " customer_debt_payment records with test data");
                }
            } catch (Exception e) {
                System.err.println("ℹ️  Could not update test data: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error initializing schema: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

