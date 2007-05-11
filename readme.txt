idea:
lock that allows unlocking by different thread. Semaphore can be used for that, but semaphores can't create conditions.

idea:
rwlock met lockupgrade.

-file met lock om completion: zie nccw.

improvement code:
waiting on a condition under a another lock could lead to performance issues and liveness issues. The outer lock
is hold and prevents lower locks from taking place to change the condition other threads are waiting on.

idea:

improvement code:
watch out for code where the following sequence occurs:
lock1.lock, lock2.lock, conditionfromlock2.await

If this happens, lock1.lock prevents making changes when the system gets in the conditionfrom2.await. I could be a
recipe for deadlocks. This problem is present in the current threadpoolrepeater, I don't know if it happens with the
wrapping structures.

improvement test:
run the test repeatedly and if reports contain error, these reports have to be stored in a special directory
for inspection. So for every run a different directory needs to be used

improvement documentation:
uninterruptibly calls should document that they keep the interrupted status of a thread
intact.

improvement implementation
repeater with 1 thread and no arg constructor

GUIDELINE DOCUMENTATION
improvement documentation:
make clear that calling a waiting method with a 0 timeout, doesn't block. This behaviour
is used throughout the j.u.c library, but with the object.wait is has different semantics.

improvement documentation:
defaultawaitablereference en bij aanwezigheid value, wordt interrupt tripwire niet
gepasseerd, dus methode is dan niet responsive voor interrupts. performance optimalisatie.

GUIDELINE DOCUMENTATION
improvement documentation:
save handof documentatie toevoegen aan structuren die die eigenschap bezit.

GUIDELINE TESTING:
test methods that are interruptble.

improvement documentation:
process should not keep references to objects they process

improvement documentation:
for each thread, there should be a process-instance. This functionality is difficult to realize with the
current repeater. All calling threads could be placed in a map, and their processes could be placed in that
map. If no process is found for that thread, one needs to be created.

improvement consistency:
the repeater is automatically started when an item is placed, but the executor rejects
the task and is not started.

improvement design:
check out the abstractqueuedsynchronizer. It can be used to construct custom synchronization
structures.

improvement build:
add some style checking tool that is able to detect possible problems.

improvement test:
threadpoolrepeater -> actualpoolsize tests
improvement test:
threads zo maken dat ze testthread allemaal extenden en bij de join methode ook nog
controleren op runtime exceptionS.. maar wel oppassen met threads die 

improvement test:
The setPoolSizeStressTest fails on some occassion. It doesn't end.

improvement test:
StrictLendableReference_TakeBackTest.testSomeWaitingNeeded fails on some occassions.

improvement test:
ipv te werken met hardcoded times, use some global constants. This makes changing them a lot easier to change.
most testclass already are using constants.

improvement test:
alle methoden die interruptable zijn, die moeten werken als een tripwire als de interrupt
status staat. Het mag hoe dan ook niet voorkomen dat de thread of niet geinterrupt is,
of de interrupt status niet is gezet. Dus alle interruptible calls ook met een interrupt
aanroepen.

improvement test:
-allerlei tijd gerelateerd geneuzel testen. kijken of een lock bv direct beschikbaar is.
nu weet je alleen of het uiteindelijk beschikbaar is.

improvement test:
repeater/blockingexecutor threads in de pool bij het shutdownen ook echt zien stoppen.
de thread moet in de niet more alive state komen.

improvement test:
aflopende timeouts.. try calls, krijg je ook garanties over verminderen van de timeouts?

improvement test:
all methods that are not interruptable, should be tested with and without the interrupted
flag set. The tests should behave the same, apart from the interruptstatus check in the end.

improvement test:
all tests that interrupt a thread, should do the interrupting from the maintestthread
and not from the seperate thread. It makes the test more complicated, and an interrupt
call doesn't influence the calling thread.

idea:
pausenotsupportexception in case you don't want/can't deal with pause.

guidelines for implementation
- make interruptable calls the default. Uninterruptibly calls should be specially named,
like putUninterruptibly.
-for blocking methods provide alternative:
    put
    putUninterruptibly
    tryput
    tryPut with timeout
    tryPutUninterruptibly with timeout.
-methods that use a timeout (also a 0 timeout), prefix them with try.



idea improvement test:
the long calculation could used to stress a cpu, could contain yields to increase the number of context switches.