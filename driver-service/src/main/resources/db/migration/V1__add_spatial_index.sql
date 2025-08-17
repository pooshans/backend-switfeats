-- Create a spatial index on the driver_locations table
-- Using SRID 4326 (WGS84) for geographic coordinates (latitude/longitude)
CREATE INDEX idx_driver_locations_position 
ON driver_locations 
USING GIST(ST_SetSRID(ST_MakePoint(longitude, latitude), 4326));
