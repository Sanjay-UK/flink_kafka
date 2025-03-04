import org.apache.flink.api.common.functions.MapFunction;
import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Calculator for Option Greeks
 * This class calculates Delta, Gamma, Theta, Vega and Rho for options
 * using Black-Scholes model
 */
public class GreeksCalculator implements MapFunction<Option, OptionGreeks> {
    
    private final NormalDistribution normalDistribution = new NormalDistribution();
    
    @Override
    public OptionGreeks map(Option option) throws Exception {
        // Extract Option values
        double s = option.underlyingPrice;  // Underlying price
        double k = option.strike;           // Strike price
        double t = option.timeToExpiry;     // Time to expiry in years
        double r = option.riskFreeRate;     // Risk-free rate
        double v = option.impliedVolatility; // Implied volatility
        boolean isCall = "CALL".equalsIgnoreCase(option.type);
        
        // Calculate d1 and d2 (Black-Scholes parameters)
        double d1 = (Math.log(s / k) + (r + 0.5 * v * v) * t) / (v * Math.sqrt(t));
        double d2 = d1 - v * Math.sqrt(t);
        
        // Calculate cumulative normal distribution values
        double nd1 = normalDistribution.cumulativeProbability(d1);
        double nd2 = normalDistribution.cumulativeProbability(d2);
        
        // Calculate normal PDF of d1 (needed for gamma and vega)
        double pd1 = normalDistribution.density(d1);
        
        // Calculate option greeks
        double delta = isCall ? nd1 : nd1 - 1;
        double gamma = pd1 / (s * v * Math.sqrt(t));
        
        // Theta (time decay) - divide by 365 to get daily theta
        double theta;
        if (isCall) {
            theta = (-s * v * pd1) / (2 * Math.sqrt(t)) - r * k * Math.exp(-r * t) * nd2;
        } else {
            theta = (-s * v * pd1) / (2 * Math.sqrt(t)) + r * k * Math.exp(-r * t) * (1 - nd2);
        }
        theta = theta / 365.0; // Daily theta
        
        // Vega (sensitivity to volatility) - divided by 100 for 1% change
        double vega = 0.01 * s * Math.sqrt(t) * pd1;
        
        // Rho (sensitivity to interest rate) - divided by 100 for 1% change
        double rho;
        if (isCall) {
            rho = 0.01 * k * t * Math.exp(-r * t) * nd2;
        } else {
            rho = -0.01 * k * t * Math.exp(-r * t) * (1 - nd2);
        }
        
        // Create OptionGreeks object
        return new OptionGreeks(
            option.symbol,
            option.underlyingSymbol,
            option.type,
            option.strike,
            option.underlyingPrice,
            option.optionPrice,
            option.impliedVolatility,
            option.timeToExpiry,
            option.riskFreeRate,
            delta,
            gamma,
            theta,
            vega,
            rho
        );
    }
}