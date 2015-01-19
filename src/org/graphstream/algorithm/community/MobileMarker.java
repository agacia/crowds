package org.graphstream.algorithm.community;

public enum MobileMarker {
X("x"), 
Y("y"),
WEIGHT("weight"),
MODULE("module"), 
COMMUNITY("community"), 
COMMUNITY_SCORE("community_score"), 
SPEED("speed"), 
AVG_SPEED("avg_speed"), 
LANE("lane"), 
ANGLE("angle"), 
SLOPE("slope"), 
POS("pos"), 
DISTANCE("distance"), 
DSD("dsd"), 
IDSD("idsd"), 
CONGESTION("congestion"), 
DYNAMISM("dynamism"), 
TIMEMEANSPEED("timemeanspeed"),
LINK_DURATION("linkDuration"),
MOBILITY_SIMILARITY("mobilitySimilarity");

private final String identifier;

/**
 * Constructor.
 *
 * @param identifier
 *            - identfier.
 */
private MobileMarker(String identifier)
{
    this.identifier = identifier;
}

/**
 * {@inheritDoc}
 */
public String toString()
{
    return identifier;
}

public static String getEnumNameForValue(Object value){
	MobileMarker[] values = MobileMarker.values();
 String enumValue = null;
 for(MobileMarker eachValue : values){
  enumValue =eachValue.toString();
  if( enumValue.equals(value)){
   return eachValue.name();
  }
 }
 return enumValue;
}
}