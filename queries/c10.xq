<results>
{
for $p in doc('xmlDataBaseXmark.xml')/site/people/person
let $e := $p/homepage
where count($e) = 0
return
<people_without_homepage>
{$p/name}
</people_without_homepage>
} </results>