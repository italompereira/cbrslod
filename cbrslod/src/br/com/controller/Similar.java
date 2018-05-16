package br.com.controller;
import br.com.controller.Instance;

public class Similar {
	public Instance instance;
	public double sim;
	
	public Similar(Instance instance, double sim) {
		super();
		this.instance = instance;
		this.sim = sim;
	}

	public Instance getInstance() {
		return instance;
	}

	public void setInstance(Instance instance) {
		this.instance = instance;
	}

	public double getSim() {
		return sim;
	}

	public void setSim(double sim) {
		this.sim = sim;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.instance + " " + this.sim;
	}
	
	
}
