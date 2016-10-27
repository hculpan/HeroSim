package org.culpan.herosim.plugin.bonjour;

import com.apple.dnssd.DNSSD;
import com.apple.dnssd.DNSSDException;
import com.apple.dnssd.ResolveListener;

public class DiscoveredInstance {
	private int ind;
	private String name, domain, serviceType;

	public DiscoveredInstance(String serviceType, int i, String n, String d) {
		ind = i;
		name = n;
		domain = d;
		this.serviceType = serviceType;
	}

	public String toString() {
		String i = DNSSD.getNameForIfIndex(ind);
		return (i + " " + name + " (" + domain + ")");
	}

	public void resolve(ResolveListener x) {
		try {
			DNSSD.resolve(0, ind, name, serviceType, domain, x);
		} catch (DNSSDException e) {
			e.printStackTrace();
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getDomain() {
		return domain;
	}
}

