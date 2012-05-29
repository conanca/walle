package com.dolplay.walle;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ HttpGetTest.class, HttpPostTest.class, SSLTest.class, ShutdownTest.class, CurrentHeaderTest.class })
public class AllTests {

}