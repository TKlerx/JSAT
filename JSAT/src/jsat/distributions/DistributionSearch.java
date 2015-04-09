
package jsat.distributions;

import java.util.Arrays;

import org.apache.commons.math3.util.Precision;

import jsat.distributions.empirical.KernelDensityEstimator;
import jsat.linear.Vec;
import jsat.testing.goodnessoffit.KSTest;
import jsat.utils.Pair;

/**
 * Provides methods for selecting the distribution that best fits a given data set. 
 * @author Edward Raff
 */
public class DistributionSearch
{
    private static Distribution[] possibleDistributions = new Distribution[] 
    { 
        new Normal(), 
        new LogNormal(), new Exponential(),
        new Gamma(2, 1), new FisherSendor(10, 10), new Weibull(2, 1), 
        new Uniform(0, 1), new Logistic(3, 2), new MaxwellBoltzmann(), 
        new Pareto(), new Rayleigh(2)
    };
    
    /**
     * Searches the distributions that are known for a possible fit, and returns 
     * what appears to be the best fit. 
     * 
     * @param v all the values from a sample
     * @return the distribution that provides the best fit to the data that this method could find.
     */
    public static Distribution getBestDistribution(Vec v)
    {
        return getBestDistribution(v, possibleDistributions);
    }
    
    /**
     * Searches the distributions that are known for a possible fit, and returns 
     * what appears to be the best fit. If no suitable fit can be found, a 
     * {@link KernelDensityEstimator} is fit to the data. 
     * 
     * @param v all the values from a sample
     * @param KDECutOff the cut off value used for using the KDE. Should be in 
     * the range (0, 1). Values less than zero means the KDE will never be used,
     * and greater then 1 means the KDE will always be used. 
     * @return the distribution that provides the best fit to the data that this method could find.
     */
    public static Distribution getBestDistribution(Vec v, double KDECutOff)
    {
        return getBestDistribution(v, KDECutOff, possibleDistributions);
    }
    
    /**
     * Searches the distributions that are given for a possible fit, and returns 
     * what appears to be the best fit. 
     * 
     * @param v all the values from a sample
     * @param possibleDistributions the array of distribution to try and fit to the data
     * @return the distribution that provides the best fit to the data that this method could find.
     */
    public static Distribution getBestDistribution(Vec v, Distribution... possibleDistributions)
    {
        return getBestDistribution(v, 0.0, possibleDistributions);
    }
    
    /**
     * Searches the distributions that are given for a possible fit, and returns 
     * what appears to be the best fit. If no suitable fit can be found, a 
     * {@link KernelDensityEstimator} is fit to the data. 
     * 
     * @param v all the values from a sample
     * @param KDECutOff the cut off value used for using the KDE. Should be in 
     * the range (0, 1). Values less than zero means the KDE will never be used,
     * and greater then 1 means the KDE will always be used. 
     * @param possibleDistributions the array of distribution to try and fit to the data
     * @return  the distribution that provides the best fit to the data that this method could find.
     */
    public static Distribution getBestDistribution(Vec v, double KDECutOff, Distribution... possibleDistributions)
    {
        if(v.length() == 0)
            throw new ArithmeticException("Can not fit a distribution to an empty set");
		Pair<Boolean, Double> result = checkForDifferentValues(v);
		if(result.getFirstItem()){
			return new SingleValueDistribution(result.getSecondItem());
		}
        //Thread Safety, clone the possible distributions
        
        Distribution[] possDistCopy = new Distribution[possibleDistributions.length];
        
        for(int i = 0; i < possibleDistributions.length; i++)
            possDistCopy[i] = possibleDistributions[i].clone();
        
        
        KSTest ksTest = new KSTest(v);
        
        Distribution bestDist = null;
        double bestProb = 0;
        
        for(Distribution cd : possDistCopy)
        {
            try
            {
                cd.setUsingData(v);
                double prob = ksTest.testDist(cd);
                
                if(prob > bestProb)
                {
                    bestDist = cd;
                    bestProb = prob;
                }
                
            }
            catch(Exception ex)
            {
                
            }
        }
        
        ///Return the best distribution, or if somehow everythign went wrong, a normal distribution
        try
        {
            if(bestProb >= KDECutOff)
                return bestDist == null ? new Normal(v.mean(), v.standardDeviation()) : bestDist.clone();
            else
                return new KernelDensityEstimator(v);
        }
        catch (RuntimeException ex)//Mostly likely occurs if all values are all zero
        {
            if(v.standardDeviation() == 0)
                return null;
            throw new ArithmeticException("Catistrophic faulure getting a distribution");
        }
    }
	/**
	 * True iff there are only identical values in the vector
	 * @param v
	 * @return
	 */
	public static Pair<Boolean, Double> checkForDifferentValues(Vec v) {
		double value = v.get(0);
		for(int i = 1;i<v.length();i++){
			if(!Precision.equals(value, v.get(i))){
				return new Pair<Boolean, Double>(false, -1.0);
			}
		}
		return new Pair<Boolean, Double>(true, value);
	}

	/**
	 * search for all possible distributions and maybe also for a KDE. Does not compare bestProb to cutoff
	 * @param v
	 * @param includeKDE
	 * @return
	 */
	public static Distribution getBestDistribution(Vec v, boolean includeKDE) {
		if(!includeKDE){
			return getBestDistribution(v);
		}else{
			Distribution[] possibleDists = Arrays.copyOf(possibleDistributions, possibleDistributions.length+1);
			possibleDists[possibleDists.length-1] = new KernelDensityEstimator(v);
			return getBestDistribution(v,possibleDists);
		}
	}
}
