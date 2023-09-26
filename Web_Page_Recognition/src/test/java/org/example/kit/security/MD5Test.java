package org.example.kit.security;

import junit.framework.TestCase;
import org.junit.Test;

public class MD5Test extends TestCase {
    @Test
    public void testGetK() {
        for (int i : MD5.getK()) {
            System.out.println(i);
            System.out.printf("0x%08x\n",i);
        }
    }
}