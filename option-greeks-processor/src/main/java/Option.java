import java.util.Objects;

public class Option {
  public String symbol;         // Option symbol
  public String underlyingSymbol; // Underlying stock symbol
  public String type;           // "CALL" or "PUT"
  public Double strike;         // Strike price
  public Double underlyingPrice; // Current price of underlying asset
  public Double optionPrice;    // Current option premium
  public Double impliedVolatility; // IV
  public Double timeToExpiry;   // Time to expiration in years
  public Double riskFreeRate;   // Risk-free interest rate

  public Option() {}

  public Option(
      String symbol, 
      String underlyingSymbol,
      String type, 
      Double strike, 
      Double underlyingPrice, 
      Double optionPrice, 
      Double impliedVolatility,
      Double timeToExpiry,
      Double riskFreeRate) {
    this.symbol = symbol;
    this.underlyingSymbol = underlyingSymbol;
    this.type = type;
    this.strike = strike;
    this.underlyingPrice = underlyingPrice;
    this.optionPrice = optionPrice;
    this.impliedVolatility = impliedVolatility;
    this.timeToExpiry = timeToExpiry;
    this.riskFreeRate = riskFreeRate;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Option{");
    sb.append("symbol='").append(symbol).append('\'');
    sb.append(", underlyingSymbol='").append(underlyingSymbol).append('\'');
    sb.append(", type='").append(type).append('\'');
    sb.append(", strike=").append(String.valueOf(strike));
    sb.append(", underlyingPrice=").append(String.valueOf(underlyingPrice));
    sb.append(", optionPrice=").append(String.valueOf(optionPrice));
    sb.append(", impliedVolatility=").append(String.valueOf(impliedVolatility));
    sb.append(", timeToExpiry=").append(String.valueOf(timeToExpiry));
    sb.append(", riskFreeRate=").append(String.valueOf(riskFreeRate));
    sb.append('}');
    return sb.toString();
  }

  public int hashCode() {
    return Objects.hash(
        super.hashCode(), 
        symbol, 
        underlyingSymbol,
        type, 
        strike, 
        underlyingPrice, 
        optionPrice, 
        impliedVolatility,
        timeToExpiry,
        riskFreeRate);
  }
}