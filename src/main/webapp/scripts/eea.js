jQuery(document).ready(function(){
    $('#tabbedmenu').removeAttr('id');
    $('.wrapper-browse-list').css('overflow', 'visible');
    
    $('#region-content div.pagination .firstpage, #region-content div.pagination .prevpage')
	.removeClass('firstpage prevpage')
	.addClass('listingPrevious');

    $('#region-content div.pagination .selectedpage')
	.removeClass('selectedpage')
	.addClass('current');

    $('#region-content div.pagination .nextpage, #region-content div.pagination .lastpage')
	.removeClass('nextpage lastpage')
	.addClass('next');

    $('#region-content div.pagination').removeClass('pagination').addClass('listingBar');
    $('.pagebanner, .pagelinks').wrapAll('<div class="listingBar" />');

    $('#resourcesResultList').removeClass('sortable').addClass('listing');
    $('#dataset').removeClass('sortable').addClass('listing');
    $('#listItem').removeClass('datatable').addClass('listing');
    $('#harvestSource').removeClass('sortable').addClass('listing');
    $('#queueItems').removeClass('sortable').addClass('listing');
    $('#registratioinslist').removeClass('datatable').addClass('listing');
    $('#uploadsForm table.datatable').addClass('listing');
    $('#region-content table.datatable').addClass('listing');

    $('select#format').parent().css({'position':'initial','float':'left','margin-top':'5px'});
    $('input#nrOfHits').parent().css({'position':'initial','float':'left','margin':'5px 0 0 5px'});
    $('input#owlsameas').parent().css({'position':'initial','float':'left','margin':'5px 0 0 5px'});
    $('#executeButton').parent().css({'position':'initial','float':'right','margin-top':'5px'});
});