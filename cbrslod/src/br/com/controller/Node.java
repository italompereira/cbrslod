package br.com.controller;

import java.io.Serializable;

public class Node implements Serializable {

	private static final long serialVersionUID = 1L;
	private String node;
	private String typeOfNode;
	
	public Node(String node, String typeOfNode) {
		super();
		this.node = node;
		this.typeOfNode = typeOfNode;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public String getTypeOfNode() {
		return typeOfNode;
	}

	public void setTypeOfNode(String typeOfNode) {
		this.typeOfNode = typeOfNode;
	}

	@Override
	public String toString() {
		return node;
	}
}
