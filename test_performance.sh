#!/bin/bash

##############################################################
# Performance Testing Script for Netty Migration
# 
# Usage: ./test_performance.sh
#
# Requirements:
# - jps, jstack, jmap (included in JDK)
# - ps, top, netstat (Linux/Mac)
##############################################################

echo "╔════════════════════════════════════════════════════════╗"
echo "║     🔍 NETTY PERFORMANCE TESTING SCRIPT 🔍            ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Find Java process
echo -e "${BLUE}[1/6] Finding Java process...${NC}"
PID=$(jps | grep -i "ngocrong\|server" | awk '{print $1}' | head -1)

if [ -z "$PID" ]; then
    echo -e "${RED}❌ Server not running! Start server first.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Found server process: PID $PID${NC}"
echo ""

# Check threads
echo -e "${BLUE}[2/6] Checking thread count...${NC}"
if command -v ps &> /dev/null; then
    THREADS=$(ps -Lf -p $PID 2>/dev/null | wc -l)
    THREADS=$((THREADS - 1)) # Remove header
    
    echo "Thread count: $THREADS"
    
    if [ $THREADS -lt 50 ]; then
        echo -e "${GREEN}✅ EXCELLENT! Netty is working (expected: 29-40)${NC}"
    elif [ $THREADS -lt 100 ]; then
        echo -e "${YELLOW}⚠️  Good, but could be better (expected: 29-40)${NC}"
    else
        echo -e "${RED}❌ TOO MANY THREADS! Netty may not be active (expected: 29-40)${NC}"
        echo -e "${YELLOW}   Old network uses 80+ threads${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  'ps' command not available, skipping...${NC}"
fi
echo ""

# Check memory
echo -e "${BLUE}[3/6] Checking memory usage...${NC}"
if command -v jmap &> /dev/null; then
    HEAP=$(jmap -heap $PID 2>/dev/null | grep "used" | head -1 | awk '{print $3}')
    
    if [ -n "$HEAP" ]; then
        echo "Heap memory: $HEAP bytes"
        
        # Convert to MB
        HEAP_MB=$((HEAP / 1024 / 1024))
        echo "Heap memory: ${HEAP_MB} MB"
        
        if [ $HEAP_MB -lt 200 ]; then
            echo -e "${GREEN}✅ EXCELLENT! Low memory usage${NC}"
        elif [ $HEAP_MB -lt 500 ]; then
            echo -e "${YELLOW}⚠️  Moderate memory usage${NC}"
        else
            echo -e "${RED}❌ HIGH memory usage!${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️  Could not read heap info${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  'jmap' command not available, skipping...${NC}"
fi
echo ""

# Check CPU
echo -e "${BLUE}[4/6] Checking CPU usage...${NC}"
if command -v top &> /dev/null; then
    CPU=$(top -b -n 1 -p $PID 2>/dev/null | tail -1 | awk '{print $9}')
    
    if [ -n "$CPU" ]; then
        echo "CPU usage: ${CPU}%"
        
        CPU_INT=${CPU%.*}
        if [ $CPU_INT -lt 10 ]; then
            echo -e "${GREEN}✅ EXCELLENT! Low CPU usage${NC}"
        elif [ $CPU_INT -lt 30 ]; then
            echo -e "${YELLOW}⚠️  Moderate CPU usage${NC}"
        else
            echo -e "${RED}❌ HIGH CPU usage!${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️  Could not read CPU info${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  'top' command not available, skipping...${NC}"
fi
echo ""

# Check port
echo -e "${BLUE}[5/6] Checking network ports...${NC}"
if command -v netstat &> /dev/null; then
    PORT_14445=$(netstat -an 2>/dev/null | grep -i "14445" | grep -i "LISTEN")
    
    if [ -n "$PORT_14445" ]; then
        echo -e "${GREEN}✅ Port 14445 is listening${NC}"
        
        CONNECTIONS=$(netstat -an 2>/dev/null | grep "14445" | grep -i "ESTABLISHED" | wc -l)
        echo "Active connections: $CONNECTIONS"
    else
        echo -e "${RED}❌ Port 14445 is not listening!${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  'netstat' command not available, skipping...${NC}"
fi
echo ""

# Thread details
echo -e "${BLUE}[6/6] Analyzing thread details...${NC}"
if command -v jstack &> /dev/null; then
    NETTY_THREADS=$(jstack $PID 2>/dev/null | grep -c "netty" || echo "0")
    SENDER_THREADS=$(jstack $PID 2>/dev/null | grep -c "Sender" || echo "0")
    COLLECTOR_THREADS=$(jstack $PID 2>/dev/null | grep -c "Collector" || echo "0")
    
    echo "Netty threads: $NETTY_THREADS"
    echo "Sender threads (old): $SENDER_THREADS"
    echo "Collector threads (old): $COLLECTOR_THREADS"
    
    if [ $NETTY_THREADS -gt 0 ] && [ $SENDER_THREADS -eq 0 ]; then
        echo -e "${GREEN}✅ NETTY IS ACTIVE!${NC}"
    elif [ $SENDER_THREADS -gt 0 ]; then
        echo -e "${RED}❌ OLD NETWORK IS ACTIVE (not using Netty)${NC}"
    else
        echo -e "${YELLOW}⚠️  Cannot determine network type${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  'jstack' command not available, skipping...${NC}"
fi
echo ""

# Summary
echo "╔════════════════════════════════════════════════════════╗"
echo "║                    📊 SUMMARY                         ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""

if [ $NETTY_THREADS -gt 0 ] && [ $THREADS -lt 50 ]; then
    echo -e "${GREEN}🎉 CONGRATULATIONS!${NC}"
    echo -e "${GREEN}   Netty is working correctly!${NC}"
    echo ""
    echo "Expected performance:"
    echo "  ✅ Threads: 29-40"
    echo "  ✅ Memory: <200 MB (idle)"
    echo "  ✅ CPU: <10% (idle)"
    echo "  ✅ Latency: 5-10ms"
elif [ $SENDER_THREADS -gt 0 ]; then
    echo -e "${RED}⚠️  WARNING!${NC}"
    echo -e "${RED}   Old network is still active!${NC}"
    echo ""
    echo "To enable Netty:"
    echo "1. Edit ServerManager.java"
    echo "2. Change: useOldNetwork() → useNettyNetwork()"
    echo "3. Rebuild & restart server"
else
    echo -e "${YELLOW}⚠️  Cannot determine status${NC}"
    echo ""
    echo "Manual check:"
    echo "  jstack $PID | grep -i netty"
    echo "  jstack $PID | grep -i sender"
fi

echo ""
echo "╔════════════════════════════════════════════════════════╗"
echo "║              💡 DETAILED REPORTS                      ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""
echo "Generate detailed reports:"
echo ""
echo "1. Thread dump:"
echo "   jstack $PID > thread_dump.txt"
echo ""
echo "2. Heap dump:"
echo "   jmap -heap $PID > heap_info.txt"
echo ""
echo "3. Memory histogram:"
echo "   jmap -histo $PID | head -30 > memory_histogram.txt"
echo ""
echo "4. GC stats:"
echo "   jstat -gc $PID 1000 10 > gc_stats.txt"
echo ""
echo -e "${BLUE}Done! Check files above for detailed analysis.${NC}"
echo ""
