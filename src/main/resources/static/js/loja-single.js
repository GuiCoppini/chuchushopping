window.onload = function() {
    buildHtmlInfo();
};


function buildHtmlInfo() {
    const urlParams = new URLSearchParams(window.location.search);
    const storeLabel = urlParams.get('loja');

    var storeInfo = getStoreInfo(storeLabel);

    // Titulo
    $('#store-label').text('Loja: ' + storeLabel);

    buildStoreContact(storeInfo);
    buildProductsList(storeInfo);
}

function buildStoreContact(storeInfo) {
    var html = [
        '<div class="col-lg-4 col-md-6 mb-4">\n',
          '<p><b>Telefone</b>: ' + storeInfo.telefone + '</p>',
          '<p><b>Website</b>: ' + storeInfo.website + '</p>',
        '</div>\n']


    $('#store-info').append(html.join(''));
}

function buildProductsList(storeInfo) {
    let products = storeInfo.produtos;

    var arrayLength = products.length;
    for (var i = 0; i < arrayLength; i++) {
        var imagePath = 'img/'+ products[i].name.split(" ").join("_") + '.jpg';
        var html = [
        '<div class="col-lg-4 col-md-6 mb-4">\n',
          '<div class="card h-100">\n',
            '<a href="#"><img class="card-img-top" src="' + imagePath + '" height=253 width=144 alt=""></a>\n',
            '<div class="card-body">\n',
              '<h4 class="card-title">\n',
                '<a href="#">' + products[i].name + '</a>\n',
              '</h4>\n',
              '<h5>R$'+ products[i].price +'</h5>\n',
              '<h5>Categoria: '+ products[i].category+'</h5>\n',
            '</div>\n',
            '<div class="card-footer">\n',
              '<small class="text-muted">&#9733; &#9733; &#9733; &#9733; &#9734;</small>\n',
            '</div>\n',
          '</div>\n',
        '</div>\n']


        $('#store-products').append(html.join(''));
    }
}