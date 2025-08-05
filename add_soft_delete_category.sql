-- ✅ ADD SOFT DELETE COLUMNS TO CATEGORIES TABLE
-- Migration: Add soft delete functionality to categories

-- Add deleted column (default false)
ALTER TABLE categories 
ADD COLUMN deleted BOOLEAN DEFAULT FALSE;

-- Add deleted_at column
ALTER TABLE categories 
ADD COLUMN deleted_at DATETIME2;

-- Add deleted_by column (foreign key to users table)
ALTER TABLE categories 
ADD COLUMN deleted_by BIGINT;

-- Add foreign key constraint
ALTER TABLE categories 
ADD CONSTRAINT FK_categories_deleted_by 
FOREIGN KEY (deleted_by) REFERENCES users(id);

-- Update existing categories to have deleted = false
UPDATE categories 
SET deleted = FALSE 
WHERE deleted IS NULL;

-- Create index for better performance
CREATE INDEX IX_categories_deleted ON categories(deleted);

-- Create index for deleted_at for cleanup queries
CREATE INDEX IX_categories_deleted_at ON categories(deleted_at);

-- Add comment for documentation
EXEC sp_addextendedproperty 
    @name = N'MS_Description',
    @value = N'Soft delete flag for categories',
    @level0type = N'SCHEMA',
    @level0name = N'dbo',
    @level1type = N'TABLE',
    @level1name = N'categories',
    @level2type = N'COLUMN',
    @level2name = N'deleted';

EXEC sp_addextendedproperty 
    @name = N'MS_Description',
    @value = N'Timestamp when category was soft deleted',
    @level0type = N'SCHEMA',
    @level0name = N'dbo',
    @level1type = N'TABLE',
    @level1name = N'categories',
    @level2type = N'COLUMN',
    @level2name = N'deleted_at';

EXEC sp_addextendedproperty 
    @name = N'MS_Description',
    @value = N'User who soft deleted the category',
    @level0type = N'SCHEMA',
    @level0name = N'dbo',
    @level1type = N'TABLE',
    @level1name = N'categories',
    @level2type = N'COLUMN',
    @level2name = N'deleted_by'; 