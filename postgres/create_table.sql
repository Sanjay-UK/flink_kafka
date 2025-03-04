-- Create options table to store option data
CREATE TABLE options (
    id SERIAL PRIMARY KEY,
    symbol VARCHAR(50) NOT NULL,
    underlying_symbol VARCHAR(10) NOT NULL,
    option_type VARCHAR(4) NOT NULL CHECK (option_type IN ('CALL', 'PUT')),
    strike DECIMAL(10, 2) NOT NULL,
    underlying_price DECIMAL(10, 2) NOT NULL,
    option_price DECIMAL(10, 4) NOT NULL,
    implied_volatility DECIMAL(10, 6) NOT NULL,
    time_to_expiry DECIMAL(10, 6) NOT NULL,  -- In years
    risk_free_rate DECIMAL(6, 4) NOT NULL,
    last_updated TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(symbol)
);

-- Create option_greeks table to store calculated greeks
CREATE TABLE option_greeks (
    id SERIAL PRIMARY KEY,
    symbol VARCHAR(50) NOT NULL,
    underlying_symbol VARCHAR(10) NOT NULL,
    option_type VARCHAR(4) NOT NULL CHECK (option_type IN ('CALL', 'PUT')),
    strike DECIMAL(10, 2) NOT NULL,
    underlying_price DECIMAL(10, 2) NOT NULL,
    option_price DECIMAL(10, 4) NOT NULL,
    implied_volatility DECIMAL(10, 6) NOT NULL,
    time_to_expiry DECIMAL(10, 6) NOT NULL,
    risk_free_rate DECIMAL(6, 4) NOT NULL,
    
    -- Greeks
    delta DECIMAL(10, 6) NOT NULL,
    gamma DECIMAL(10, 6) NOT NULL,
    theta DECIMAL(10, 6) NOT NULL,
    vega DECIMAL(10, 6) NOT NULL,
    rho DECIMAL(10, 6) NOT NULL,
    
    calculated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create index for faster lookups
CREATE INDEX idx_option_greeks_symbol ON option_greeks(symbol);
CREATE INDEX idx_option_greeks_underlying ON option_greeks(underlying_symbol);
CREATE INDEX idx_option_greeks_calculated_at ON option_greeks(calculated_at);