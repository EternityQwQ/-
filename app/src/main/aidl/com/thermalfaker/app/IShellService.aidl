package com.thermalfaker.app;

interface IShellService {
    void destroy() = 16777114;
    void exit() = 1;
    String executeCommand(String command) = 2;
}
