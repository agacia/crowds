/*
 * Copyright 2006 - 2013
 *     Stefan Balev     <stefan.balev@graphstream-project.org>
 *     Julien Baudry    <julien.baudry@graphstream-project.org>
 *     Antoine Dutot    <antoine.dutot@graphstream-project.org>
 *     Yoann Pigné      <yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin   <guilhelm.savin@graphstream-project.org>
 * 
 * This file is part of GraphStream <http://graphstream-project.org>.
 * 
 * GraphStream is a library whose purpose is to handle static or dynamic
 * graph, create them from scratch, file or any source and display them.
 * 
 * This program is free software distributed under the terms of two licenses, the
 * CeCILL-C license that fits European law, and the GNU Lesser General Public
 * License. You can  use, modify and/ or redistribute the software under the terms
 * of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
 * URL <http://www.cecill.info> or under the terms of the GNU LGPL as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C and LGPL licenses and that you accept their terms.
 */
package org.graphstream.algorithm.measure;

import java.util.HashSet;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.graphstream.graph.Node;

/**
 * Computes and updates an absolute measure based on the current community
 * assignment on a given graph as it evolves.
 * 
 * @reference M. E. Newman and M. Girvan, “Finding and Evaluating Community
 *            Structure in Networks,” <i>Physical Review E (Statistical,
 *            Nonlinear, and Soft Matter Physics)</i>, vol. 69, no. 2, pp. 026
 *            113+, Feb 2004.
 * 
 * @author Guillaume-Jean Herbiet
 */
public class MobileCommunityMeasure extends CommunityMeasure {
	
	
	/**
	 * Average marker value of the currently generated communities.
	 */
	protected float avgValue = 0;

	/**
	 * Standard deviation of the marker value of currently generated communities.
	 */
	protected float stdevValue = 0;
	protected float avgStddev = 0;
	
	protected String mobMarker = null;
	
	/**
	 * New measure algorithm with a given marker for communities.
	 * 
	 * @param marker
	 *            name of the attribute marking the communities.
	 * 
	 * @param mobMarker
	 *            name of the attribute marking the communities mobile.
	 */
	public MobileCommunityMeasure(String marker, String mobMarker) {
		super(marker);
		this.mobMarker = mobMarker;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.Algorithm#compute()
	 */
	public void compute() {
		if (graphChanged) {
			// Default measure is the number of communities
			M = (float) communities.size();

			double[] avgValueDistribution = new double[(int) M];
			double[] stdValueDistribution = new double[(int) M];
//			double[] sizeDistribution = new double[(int) M];
			int k = 0;
			
			for (Object c : communities.keySet()) {
				
				HashSet<Node> nodes = communities.get(c);
				int size = (int) nodes.size();
				double[] valueDistribution = new double[size];
				int j = 0;
				Mean mean = new Mean();
				StandardDeviation stdev = new StandardDeviation();
//				System.out.println("com " + c + ", nodes "+ size);
				for (Node n : nodes) {
					valueDistribution[j++] = n.getAttribute(this.mobMarker) == null ? 0.0 : (Double)n.getAttribute(this.mobMarker);
				}
				// Compute the statistical moments
				float avgValue = (float) mean.evaluate(valueDistribution);
				float stdevValue = (float) stdev.evaluate(valueDistribution);

				avgValueDistribution[k] = avgValue;
				stdValueDistribution[k] = stdevValue;
				++k;
			}

			// Compute the statistical moments
			Mean mean = new Mean();
			StandardDeviation stdev = new StandardDeviation();
			avgValue = (float) mean.evaluate(avgValueDistribution);
			stdevValue = (float) stdev.evaluate(avgValueDistribution);
			avgStddev = (float) mean.evaluate(stdValueDistribution);
			graphChanged = false;
		}
		
	}

	/**
	 * Compute the average marker value in communities
	 * 
	 * @return Average marker value
	 */
	public float averageValue() {
		return avgValue;
	}
	
	public float averageStddev() {
		return avgStddev;
	}

	/**
	 * Compute the standard deviation of the average marker value in communities
	 * 
	 * @return Standard deviation of the average marker value in communities
	 */
	public float stdev() {
		return stdevValue;
	}

}
