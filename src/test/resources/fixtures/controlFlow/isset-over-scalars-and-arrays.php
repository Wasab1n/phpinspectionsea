<?php

$array = [];
if (isset(<warning descr="Concatenation is used as an index, should be moved to a variable">$array['0' . 1]</warning>)) {
    /* multiple isset, we do nothing to not xonflict with other inspections */
    $a = isset($array['index'], $array['index']);

    /* generally used constructs we'll not report */
    $b = isset($array['index']) ? $array['index'] : null;
    $b = !isset($array['index']) ? null : $array['index'];

    echo isset(<weak_warning descr="Probably 'array_key_exists(...)' construction should be used for better data *structure* control">$array[$index]</weak_warning>);
    echo !isset(<weak_warning descr="Probably 'array_key_exists(...)' construction should be used for better data *structure* control">$array[$index]</weak_warning>);

    /* if isset value used, we only suggesting to use array_key_exists */
    $tmp []= isset(<weak_warning descr="Probably 'array_key_exists(...)' construction should be used for better data *structure* control">$array['property']</weak_warning>);
    $tmp []= !isset(<weak_warning descr="Probably 'array_key_exists(...)' construction should be used for better data *structure* control">$array['property']</weak_warning>);
    $x = isset(<weak_warning descr="Probably 'array_key_exists(...)' construction should be used for better data *structure* control">$array['0' . 1]</weak_warning>);
    $x = !isset(<weak_warning descr="Probably 'array_key_exists(...)' construction should be used for better data *structure* control">$array['0' . 1]</weak_warning>);
    return isset(<weak_warning descr="Probably 'array_key_exists(...)' construction should be used for better data *structure* control">$array['0' . 1]</weak_warning>);
}

$x = isset(<weak_warning descr="Probably it can be 'null !== $array' construction used instead">$array</weak_warning>);
$y = !isset(<weak_warning descr="Probably it can be 'null === $array' construction used instead">$array</weak_warning>);