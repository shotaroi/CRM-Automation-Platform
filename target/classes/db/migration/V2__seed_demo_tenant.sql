-- Seed demo tenant for development/testing
INSERT INTO tenant (id, name) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Demo Tenant')
ON CONFLICT (id) DO NOTHING;
