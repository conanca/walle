package com.dolplay.walle;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ WalleHttpClientTest.class, SSLTest.class })
public class AllTests {

}