<?php

/* a pitfall: instanceof <trait> returns false */
trait IOAT_Trait {}
$x = <error descr="instanceof against traits returns 'false'.">$z instanceof IOAT_Trait</error>;
$x = <error descr="instanceof against traits returns 'false'.">$z instanceof IOAT_Trait::class</error>;
$x = <error descr="instanceof against traits returns 'false'.">$z instanceof \IOAT_Trait</error>;

/* a typo: comparison instead of assignment */
$a <error descr="It seems that '=' should be here.">==</error> $b;

/* a typo: greater or equal instead of has element definition */
$a = ['x' <error descr="It seems that '=>' should be here.">>=</error> 'y'];
$a = [$x >= 'y']; // <- left operand is not a string, hence not reported