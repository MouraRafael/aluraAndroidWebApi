package br.com.alura.estoque.repository;

import java.io.IOException;
import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
import br.com.alura.estoque.retrofit.service.ProdutoService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProdutoRepository {
    private final ProdutoDAO dao;
    private final ProdutoService service;

    public ProdutoRepository(ProdutoDAO dao) {
        this.dao = dao;
        service = new EstoqueRetrofit().getProdutoService();
    }


    //metodos de busca de lista de produtos
    public void buscaProdutos(DadosCarregadosListener<List<Produto>> listener) {


        buscaProdutosInternos(listener);
    }

    private void buscaProdutosInternos(DadosCarregadosListener<List<Produto>> listener) {
        new BaseAsyncTask<>(dao::buscaTodos,
                resultado -> {
                    // listener para notificar o adapter de mudancas
                    listener.quandoCarregados(resultado);

                    buscaProdutosNaAPI(listener);

                })
                .execute();
    }

    private void buscaProdutosNaAPI(DadosCarregadosListener<List<Produto>> listener) {

        Call<List<Produto>> call = service.buscaTodos();

        new BaseAsyncTask<>(() -> {
            try {
                Response<List<Produto>> resposta = call.execute();
                List<Produto> produtosNovos = resposta.body();
                dao.salva(produtosNovos);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return dao.buscaTodos();

        }, listener::quandoCarregados
        ).executeOnExecutor(BaseAsyncTask.THREAD_POOL_EXECUTOR);
    }
    // fim metodos de busca de lista de produtos

    public void salva(Produto produto, DadosCarregadosListener<Produto> listener) {
        Call<Produto> call = service.salva(produto);
        call.enqueue(new Callback<Produto>() {
            @Override
            public void onResponse(Call<Produto> call, Response<Produto> response) {
                Produto produtoQueFoiSalvo = response.body();
                new BaseAsyncTask<>(() -> {
                    long id = dao.salva(produtoQueFoiSalvo);
                    return dao.buscaProduto(id);
                }, produtoSalvo ->
                        //notificacao ao adapter
                        listener.quandoCarregados(produtoSalvo)
                )
                        .execute();

            }

            @Override
            public void onFailure(Call<Produto> call, Throwable t) {

            }
        });


    }

    // metodos de salvamento de produtos

    public interface DadosCarregadosListener<T> {
        void quandoCarregados(T produtos);
    }

}
