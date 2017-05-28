package tests;

import java.awt.image.BufferedImage;

import boofcv.alg.misc.ImageStatistics;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayU8;

/**
 * Idea:
 * To calculate the variance, we need to calculate the mean, then the variance.
 * Which results in a complexity of imageWidth*imageHeight*2*const
 * 
 * For U8, we can use the fact that the number of histogram buckets equals the number of colors.
 * We can therefore calculate the mean and the variance in
 * histogramCosts := imageWidth*imageHeight*const
 * meanCosts := 256*const
 * varianceCosts := 256*const
 * Overall costs: histogramCosts + meanCosts + varianceCosts <=> imageWidth*imageHeight*const + 2*256*const
 * 
 * Also, we get the histogram for free.
 * 
 * @author Marius Orfgen
 *
 */
public class GrayU8Variance_V2 {

	public static void main(String[] args) {
				
//		BufferedImage image = UtilImageIO.loadImage("images/lena.png");
//		BufferedImage image = UtilImageIO.loadImage("images/portrait_1024.jpg");
		BufferedImage image = UtilImageIO.loadImage("images/portrait_2048.jpg");
//		BufferedImage image = UtilImageIO.loadImage("images/portrait_4096.jpg");
		
		GrayU8 gray = ConvertBufferedImage.convertFrom(image, (GrayU8)null);

		calculateVarianceBoofCV(gray);
		
		calculateVarianceHistogram(gray);
	}
	
	private static void calculateVarianceBoofCV(GrayU8 result) {
		System.out.println("BoofCV");
		long start = System.nanoTime();

		double mean = ImageStatistics.mean(result);
		double variance = ImageStatistics.variance(result, mean);
		
		long end = System.nanoTime();
		
		System.out.println("\tMean: "+mean);
		System.out.println("\tVariance: "+variance);
		System.out.println("\tTime: "+(end-start));
	}
	
	private static void calculateVarianceHistogram(GrayU8 result) {
		System.out.println("Histogram");
		long start = System.nanoTime();
		
		int[] histogram = new int[256];
		ImageStatistics.histogram(result, histogram);
		
		int numPixels = result.width*result.height;
		
		double mean = mean(histogram, numPixels);
		double variance = variance(histogram, mean, numPixels);
		
		long end = System.nanoTime();
		
		System.out.println("\tMean: "+mean);
		System.out.println("\tVariance: "+variance);
		System.out.println("\tTime: "+(end-start));
	}
	
	/**
	 * Computes the variance of pixel intensity values for a GrayU8 image represented by the given histogram.
	 * 
	 * @param histogram Histogram of a GrayU8 image.
	 * @param mean Mean of the image.
	 * @return
	 */
	public static double variance(int[] histogram, double mean) {
		return variance(histogram, mean, countValues(histogram));
	}
	
	/**
	 * Computes the variance of pixel intensity values for a GrayU8 image represented by the given histogram.
	 * 
	 * @param histogram Histogram of a GrayU8 image.
	 * @param mean Mean of the image.
	 * @param numValues Number of pixels in the image.
	 * @return
	 */
	public static double variance(int[] histogram, double mean, int numValues) {
		double sum = 0.0;
		for(int i=0;i<histogram.length;i++) {
			double d = i - mean;
			sum += (d*d) * histogram[i];
		}
		
		double variance = sum / numValues;
		return variance;
	}
	
	/**
	 * Counts the number of values inside the given histogram.
	 * 
	 * @param histogram
	 * @return Sum of all values in the histogram array.
	 */
	public static int countValues(int[] histogram) {
		int numValues = 0;
		for(int i=0;i<histogram.length;i++) {
			numValues += histogram[i];
		}
		return numValues;
	}
	
	/**
	 * Returns the mean pixel intensity for an image represented by the given histogram.
	 * 
	 * @param histogram Histogram of a GrayU8 image.
	 * @return Mean pixel intensity value
	 */
	public static double mean(int[] histogram) {		
		return mean(histogram,countValues(histogram));
	}
	
	public static double mean(int[] histogram, int numValues) {
		double sum = 0.0;
		for(int i=0;i<histogram.length;i++) {
			sum += (histogram[i]*i);
		}
		
		double mean = sum/numValues;
		return mean;
	}
	
	/**
	 * Returns the mean pixel intensity value.
	 * Also calculates the histogram of the image as a side effect, which can be retrieved by passing the parameter 'histogram'.
	 * 
	 * @param input Input image. Not modified.
	 * @param histogram (optional) Array that the histogram will be written to. Modified.
	 * @return Mean pixel intensity value
	 */
	public static double mean(GrayU8 input, int[] histogram) {
		if(histogram==null) {
			histogram = new int[256];
		}
		ImageStatistics.histogram(input, histogram);
		
		return mean(histogram, input.width*input.height);
	}
	
	/**
	 * Computes the variance of pixel intensity values inside the image.
	 * Also calculates the histogram of the image as a side effect, which can be retrieved by passing the parameter 'histogram'.
	 *
	 * @param input Input image. Not modified.
	 * @param histogram (optional) Array that the histogram will be written to. Modified. 
	 * @return Pixel variance   
	 */
	public static double variance(GrayU8 input, int[] histogram) {
		if(histogram==null) {
			histogram = new int[256];
		}
		ImageStatistics.histogram(input, histogram);
		
		int numValues = input.width*input.height;
		double mean =  mean(histogram, numValues);
		return variance(histogram, mean, numValues);
	}
}
