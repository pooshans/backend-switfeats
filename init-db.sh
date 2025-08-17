#!/bin/bash
set -e

# Create multiple databases
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE orderdb;
    CREATE DATABASE restaurantdb;
    CREATE DATABASE driverdb;
    
    GRANT ALL PRIVILEGES ON DATABASE orderdb TO postgres;
    GRANT ALL PRIVILEGES ON DATABASE restaurantdb TO postgres;
    GRANT ALL PRIVILEGES ON DATABASE driverdb TO postgres;
EOSQL
