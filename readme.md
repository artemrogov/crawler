# Search Crawler v1.0

Программа сканирует веб сайты, которые содержат страницы, содержащие строки указанные пользователем.
Он отображает адреса тех сайтов, которые соответствуют условиям поиска и записывает url адреса в которых были найденны 
совпадения со строкой поиска.

## Алгоритм работы 

1. загрузка страницы веб-сайта;
2. синтаксический анализ загруженной страницы и извлечение всех связей;
3. повторение процесса для каждой из извлеченных связей.

