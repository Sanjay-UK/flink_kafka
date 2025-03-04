import java.io.Serializable;
import java.util.Objects;

/**
 * OptionGreeks extends Option with calculated greeks values
 */
public class OptionGreeks extends Option implements Serializable {
    // Greeks
    public Double delta;      // Delta (Δ) - rate of change of option price to underlying price
    public Double gamma;      // Gamma (Γ) - rate of change of delta to underlying price
    public Double theta;      // Theta (Θ) - rate of change of option price with respect to time
    public Double vega;       // Vega - rate of change of option price with respect to volatility
    public Double rho;        // Rho (ρ) - rate of change of option price with respect to interest rate
    
    public OptionGreeks() {
        super();
    }
    
    public OptionGreeks(
            String symbol,
            String underlyingSymbol,
            String type,
            Double strike,
            Double underlyingPrice,
            Double optionPrice,
            Double impliedVolatility,
            Double timeToExpiry,
            Double riskFreeRate,
            Double delta,
            Double gamma,
            Double theta,
            Double vega,
            Double rho) {
        super(symbol, underlyingSymbol, type, strike, underlyingPrice, optionPrice,
              impliedVolatility, timeToExpiry, riskFreeRate);
        this.delta = delta;
        this.gamma = gamma;
        this.theta = theta;
        this.vega = vega;
        this.rho = rho;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.delete(sb.length() - 1, sb.length()); // Remove closing bracket
        sb.append(", delta=").append(delta);
        sb.append(", gamma=").append(gamma);
        sb.append(", theta=").append(theta);
        sb.append(", vega=").append(vega);
        sb.append(", rho=").append(rho);
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(
            super.hashCode(),
            delta,
            gamma,
            theta,
            vega,
            rho
        );
    }
}