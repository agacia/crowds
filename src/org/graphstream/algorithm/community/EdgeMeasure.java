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
package org.graphstream.algorithm.community;

import java.util.ArrayList;

import org.graphstream.algorithm.DynamicAlgorithm;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.Sink;
import org.graphstream.stream.SinkAdapter;

public class EdgeMeasure implements 
		DynamicAlgorithm {
	/**
	 * Graph being used to compute the measure or null.
	 */
	protected Graph g;
	private Sink trigger;
	private String marker;
	private Double min;
	private Double max;
	private Double mean;
	private Double sum;
	
	public Double getMin() {
		return min;
	}

	public void setMin(Double min) {
		this.min = min;
	}

	public Double getMax() {
		return max;
	}

	public void setMax(Double max) {
		this.max = max;
	}

	public Double getMean() {
		return mean;
	}

	public void setMean(Double mean) {
		this.mean = mean;
	}

	public Double getSum() {
		return sum;
	}

	public void setSum(Double sum) {
		this.sum = sum;
	}

	public EdgeMeasure(String marker) {
		this.marker = marker;
		trigger = new StepTrigger();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.DynamicAlgorithm#terminate()
	 */
	public void terminate() {
		g.removeSink(trigger);
		g = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.Algorithm#compute()
	 */
	public void compute() {
		
		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;
		mean = 0.0;
		sum = 0.0;
		int count = 0;
//		System.out.println("edge measure " + g.getStep() + marker );
		ArrayList<Edge> edgeSet = new ArrayList<Edge>(g.getEdgeSet());
		for (Edge edge : edgeSet) {
			if (edge.hasAttribute(marker)){
				Double value = (Double)edge.getAttribute(marker);
				min = Math.min(min, value);
				max = Math.max(max, value);
				sum += value;
				count ++;
			}
			else {
//				System.err.println("no attribute " + marker);
			}
		}

		mean = sum / count;
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
	 */
	public void init(Graph graph) {
		g = graph;
		g.addSink(trigger);
	}

	private class StepTrigger extends SinkAdapter {
		public void stepBegins(String sourceId, long timeId, double step) {
			compute();
		}
	}
}
