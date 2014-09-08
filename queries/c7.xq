<results>
{
for $art in doc('dblp')/dblp/article
where count($art/author) >=2
and $art/year >= 2000
return
<publication>
{$art}
</publication>
} </results>