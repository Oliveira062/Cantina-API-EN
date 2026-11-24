CREATE OR REPLACE FUNCTION block_history_changes()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Order history cannot be changed or deleted.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_history_append_only
BEFORE UPDATE OR DELETE ON order_history
FOR EACH ROW EXECUTE FUNCTION block_history_changes();
